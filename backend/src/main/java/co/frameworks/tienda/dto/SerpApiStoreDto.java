package co.frameworks.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerpApiStoreDto {
    private String id;
    private String title;
    private String address;
    private String city;
    private String phone;
    private Double rating;
    private Integer reviews;
    private String openState;
    private String thumbnail;
    private Double latitude;
    private Double longitude;
    private String placeId;
    private boolean nexoRegistrado;
}
