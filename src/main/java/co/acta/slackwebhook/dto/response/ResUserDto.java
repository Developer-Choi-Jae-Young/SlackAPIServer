package co.acta.slackwebhook.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ResUserDto {
    private String id;
    private String teamId;
    private String name;
    private boolean deleted;
    private String color;
    private String realName;
    private String tz;
    private String tzLabel;
    private String tzOffset;
    private ResProfileDto profile;
    private boolean isAdmin;
    private boolean isOwner;
    private boolean isPrimaryOwner;
    private boolean isResricted;
    private boolean isUltraRestricted;
    private boolean isBot;
    private LocalDate updated;
    private boolean isAppUser;
    private boolean has2fa;
}
