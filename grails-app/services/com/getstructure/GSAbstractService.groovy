package com.getstructure

import grails.gorm.transactions.Transactional
import groovy.sql.Sql


@Transactional
class GSAbstractService {
    /**
     * A Groovy SQL instance
     */
    Sql sql
}
