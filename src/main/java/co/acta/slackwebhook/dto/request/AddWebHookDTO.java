package co.acta.slackwebhook.dto.request;

import lombok.Data;

@Data
public class AddWebHookDTO {
    private String token;
    private String team_id;
    private String team_domain;
    private String channel_id;
    private String channel_name;
    private String user_id;
    private String user_name;
    private String command;
    private String text;
    private String api_app_id;
    private String is_enterprise_install;
    private String response_url;
    private String trigger_id;
}
