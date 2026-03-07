package co.acta.slackwebhook.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddBoardDto {
    private int parentBoardId;
    private String title;
    private String content;
    private String writer;
    private LocalDate regDate;
    private String link;
    private String ts;
}
