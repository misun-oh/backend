package ex07.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
//@NoArgsConstructor
@Builder
public class DeptDto {
    private String deptId;
    private String deptCode;
    @NonNull
    private String locationId;
}
