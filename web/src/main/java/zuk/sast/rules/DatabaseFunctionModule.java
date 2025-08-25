package zuk.sast.rules;

import zuk.sast.rules.dto.IssueDto;

import java.util.List;

/***
 * 函数建模数据集
 */
public class DatabaseFunctionModule {
    

    public static IssueDto queryIssueDtoById(String id) {
        List<IssueDto> issueDtos = DatabaseIssue.getAllIssue().stream().filter(issueDto -> issueDto.getId().equals(id)).toList();
        if(issueDtos!=null && issueDtos.size()>0){
            return issueDtos.get(0);
        }
        else {
            return null;
        }
    }


}
