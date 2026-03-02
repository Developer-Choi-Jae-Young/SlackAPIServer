package co.acta.slackwebhook.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddBoardDto {
    private String title;
    private String content;
    private String writer;
    private LocalDate regDate;
    private String link;
}
