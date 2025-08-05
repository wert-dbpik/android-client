package ru.wert.tubus_mobile.data.enums;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public enum EDraftStatus {

    VALID(0, "ДЕЙСТВУЕТ"),
    CHANGED(1, "ЗАМЕНЕН"),
    ANNULLED(2, "АННУЛИРОВАН"),
    UNKNOWN(3, "НЕИЗВЕСТЕН");

    private final Integer statusId;
    private final String statusName;

    EDraftStatus(Integer statusId, String statusName) {
        this.statusId = statusId;
        this.statusName = statusName;
    }

    /**
     *
     * @param statusId Integer
     * @return DraftStatus
     */
    public static EDraftStatus getStatusById(Integer statusId) {

        for(EDraftStatus status : EDraftStatus.values()){
            if(status.statusId.equals(statusId))
                return status;
        }
        return null;
    }


    /**
     * Возвращает список статусов документа
     * @return ObservableList<String>
     */
    public static List<String> getAllDraftsStatuses(){
        List<String> statuses = new ArrayList<>();
        for(EDraftStatus status : EDraftStatus.values()) {
            statuses.add(status.statusName);
        }
        return statuses;
    }

    /**
     * Возвращает id статуса в зависимости от его имени
     * @param statusName String
     * @return int
     */
    public static int findStatusIdByStatusName(String statusName){
        int statusId = 0;
        for(EDraftStatus status : EDraftStatus.values()) {
            if(status.getStatusName().equals(statusName))
                return statusId = status.getStatusId();
        }
        return statusId;
    }

}
