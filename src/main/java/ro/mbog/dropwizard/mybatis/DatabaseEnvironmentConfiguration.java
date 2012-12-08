package ro.mbog.dropwizard.mybatis;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: marius.bogdanescu
 * Date: 12/8/12
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseEnvironmentConfiguration {

    @NotNull
    @JsonProperty
    private String mapperPackage;

    @NotNull
    @JsonProperty
    private String Name;

    @NotNull
    @JsonProperty
    private DatabaseConfiguration database;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfiguration database) {
        this.database = database;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public void setMapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }
}
