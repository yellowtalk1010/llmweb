package vision.sast.rules.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;

@Data
public class IssueResult {
    private java.util.List<IssueDto> result = new ArrayList<>();
}
