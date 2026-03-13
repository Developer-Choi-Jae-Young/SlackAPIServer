package co.acta.slackwebhook.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * HttpServletRequest의 body는 기본적으로 한 번만 읽을 수 있어요.
 * 서명 검증 필터에서 body를 읽고 나면 컨트롤러에서 다시 읽지 못하는 문제가 생기기 때문에,
 * body를 byte[]로 미리 캐싱해두고 몇 번이든 읽을 수 있도록 래핑합니다.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // Java 8 호환: readAllBytes() 대신 ByteArrayOutputStream으로 직접 읽기
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream inputStream = request.getInputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }
        this.cachedBody = buffer.toByteArray();
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
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
