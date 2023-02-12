package com.getstructure.crm.model

class HubspotSearchFilterDTO {
    List<FilterGroupDTO> filterGroups

    class FilterDTO {
        String propertyName
        String operator
        String value
    }

    class FilterGroupDTO {
        List<FilterDTO> filters
    }

}
