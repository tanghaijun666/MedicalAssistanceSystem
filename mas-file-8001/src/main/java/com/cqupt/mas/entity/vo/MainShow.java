package com.cqupt.mas.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author 唐海军
 * @create 2022-12-11 10:41
 */

@Data
@Builder
public class MainShow {

    private String patientName;
    private String patientId;
    private String studyDate;
    private String modality;
    private String accessionNumber;
    private String seriesInstanceUID;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainShow mainShow = (MainShow) o;
        return Objects.equals(patientName, mainShow.patientName) && Objects.equals(patientId, mainShow.patientId) && Objects.equals(studyDate, mainShow.studyDate) && Objects.equals(modality, mainShow.modality) && Objects.equals(accessionNumber, mainShow.accessionNumber) && Objects.equals(seriesInstanceUID, mainShow.seriesInstanceUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientName, patientId, studyDate, modality, accessionNumber, seriesInstanceUID);
    }
}
