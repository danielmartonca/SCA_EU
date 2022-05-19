package applet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApduResponse {
    private byte[] bytes;
    private byte sw1;
    private byte sw2;
}
