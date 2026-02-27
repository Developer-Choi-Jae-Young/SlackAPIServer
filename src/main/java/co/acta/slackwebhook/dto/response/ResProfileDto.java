package co.acta.slackwebhook.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResProfileDto {
    private String avatarHash;
    private String statusText;
    private String statusEmoji;
    private String realName;
    private String displayName;
    private String realNameNormalized;
    private String displayNameNormalized;
    private String email;
    private String imageOriginal;
    @JsonProperty("image_24")
    private String image24;
    @JsonProperty("image_32")
    private String image32;
    @JsonProperty("image_48")
    private String image48;
    @JsonProperty("image_72")
    private String image72;
    @JsonProperty("image_192")
    private String image192;
    @JsonProperty("image_512")
    private String image512;
    private String team;
    private String phone;
}
