package com.opspilot.application.incident;

import com.opspilot.domain.model.Incident;

import java.util.List;
import java.util.Optional;

/**
 * 故障事件的持久化端口。
 *
 * <p>应用层只依赖这个接口，不关心底层是内存、H2 还是 PostgreSQL。
 * Day 3 使用内存实现，等业务闭环稳定后再替换为数据库实现。
 */
public interface IncidentRepository {

    /**
     * 保存故障事件。
     *
     * @throws DuplicateAlertException 如果同一个 alertId 已经创建过故障
     */
    Incident save(Incident incident);

    /** 按故障 ID 查询。 */
    Optional<Incident> findById(String incidentId);

    /** 按告警 ID 查询，用于幂等/重复告警判断。 */
    Optional<Incident> findByAlertId(String alertId);

    /** 返回当前所有故障，顺序由调用方决定。 */
    List<Incident> findAll();
}
