package id.ac.ui.cs.advprog.auth.dto.response.management;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserPageResponseData {
    List<UserSummaryResponseData> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean first;
    boolean last;
}