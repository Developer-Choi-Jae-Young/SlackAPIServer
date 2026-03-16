package co.acta.slackwebhook.filter;

import lombok.Getter;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    @Getter
    private final byte[] cachedBody;
    private Map<String, String[]> parameterMap;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream inputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(inputStream);

        fillParameterMap();
    }

    private void fillParameterMap() {
        this.parameterMap = new HashMap<>();
        this.parameterMap.putAll(super.getParameterMap());

        String contentType = getContentType();
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            String body = new String(cachedBody, StandardCharsets.UTF_8);
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length > 0) {
                    try {
                        // [수정포인트 2] URLDecoder.decode의 두 번째 인자는 String 이름(예: "UTF-8")이어야 합니다.
                        // String.valueOf(StandardCharsets.UTF_8)은 "UTF-8"이 아니라 "UTF-8" 객체의 toString() 값이 들어가서 오류가 날 수 있습니다.
                        // Java 8 이상이라면 StandardCharsets.UTF_8.name() 또는 그냥 "UTF-8"을 쓰세요.
                        String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                        String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name()) : "";

                        String[] values = parameterMap.get(key);
                        if (values == null) {
                            parameterMap.put(key, new String[]{value});
                        } else {
                            String[] newValues = Arrays.copyOf(values, values.length + 1);
                            newValues[values.length] = value;
                            parameterMap.put(key, newValues);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // [수정포인트 3] 로깅을 남기거나 예외를 던지되, 인코딩 이름이 확실하다면 거의 발생하지 않습니다.
                        throw new RuntimeException("URL Decoding failed", e);
                    }
                }
            }
        }
    }

    // getParameter, getParameterMap 등 오버라이드 로직은 잘 작성되었습니다.
    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {
            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            @Override public boolean isFinished() { return byteArrayInputStream.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {}
            @Override public int read() { return byteArrayInputStream.read(); }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}