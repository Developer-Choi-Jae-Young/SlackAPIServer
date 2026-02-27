package co.acta.slackwebhook.dto.response;

import lombok.Data;

@Data
public class ResUserInfoDto {
    private boolean ok;
    private ResUserDto user;
}
