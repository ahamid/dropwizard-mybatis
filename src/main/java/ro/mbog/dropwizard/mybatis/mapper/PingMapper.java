package ro.mbog.dropwizard.mybatis.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * Created with IntelliJ IDEA.
 * User: marius.bogdanescu
 * Date: 12/8/12
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PingMapper {

    @Select("SELECT 1")
    public int ping();
}
