package com.edw.repository;

import com.edw.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventEntityRepository extends JpaRepository<EventEntity, String> {

    @Query(value = "SELECT e.client_id as clientId, COUNT(*) as count FROM event_entity e WHERE e.realm_id = :realmId AND e.type = 'LOGIN' GROUP BY e.client_id", nativeQuery = true)
    List<Object[]> findClientLoginCounts(@Param("realmId") String realmId);

    @Query(value = """
            WITH all_hours AS (
                SELECT generate_series(0, 23) AS event_hour_kst
            ),
            hourly_events AS (
                SELECT 
                    EXTRACT(HOUR FROM TO_TIMESTAMP(event_time / 1000) AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Seoul') AS event_hour_kst
                FROM 
                    event_entity
                WHERE 
                    type = 'LOGIN'
                    AND client_id LIKE '%' || :clientIdPattern || '%'
            )
            SELECT 
                a.event_hour_kst, 
                COALESCE(COUNT(e.event_hour_kst), 0) AS event_count
            FROM 
                all_hours a
            LEFT JOIN 
                hourly_events e ON a.event_hour_kst = e.event_hour_kst
            GROUP BY 
                a.event_hour_kst
            ORDER BY 
                a.event_hour_kst
            """, nativeQuery = true)
    List<Object[]> countEventsByHour(@Param("clientIdPattern") String clientIdPattern);
}
