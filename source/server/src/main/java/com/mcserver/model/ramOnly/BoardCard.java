
package com.mcserver.model.ramOnly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCard {
    private long pairId;
    private String cardCode;
    private boolean matched;
    private String imgPath;
    private int x;
    private int y;
}
