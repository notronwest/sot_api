import groovy.sql.Sql

// Place your Spring DSL code here
beans = {
    /**
     * Provide a Groovy SQL instance tied to the application's datasources
     */
    sql( Sql, ref('dataSource') ) // read/write sql
}
