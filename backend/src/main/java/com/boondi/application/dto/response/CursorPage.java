package com.boondi.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cursor-paginated result set")
public class CursorPage<T> {

    @Schema(description = "Items in this page")
    private List<T> items;

    @Schema(description = "Cursor to pass as ?cursor= on the next request. Null when no more pages.")
    private String nextCursor;

    @Schema(description = "Whether more items exist after this page")
    private boolean hasMore;

    @Schema(description = "Number of items returned in this page")
    private int count;
}
