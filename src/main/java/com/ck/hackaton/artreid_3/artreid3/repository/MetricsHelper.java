package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.dto.BreachDistributionDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetricsHelper {

    public static String buildBucketColumns(String prefix, String thresholdParam, int[] bounds, int multiplier) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bounds.length; i++) {
            int current = bounds[i] * multiplier;
            if (i == 0) {
                sb.append(String.format("COUNT(%1$s_interval_min) FILTER (WHERE %1$s_interval_min > :%2$s AND %1$s_interval_min <= :%2$s + %3$d) as %1$s_b_%4$d, \n",
                        prefix, thresholdParam, current, i));
            } else {
                int previous = bounds[i - 1] * multiplier;
                sb.append(String.format("COUNT(%1$s_interval_min) FILTER (WHERE %1$s_interval_min > :%2$s + %3$d AND %1$s_interval_min <= :%2$s + %4$d) as %1$s_b_%5$d, \n",
                        prefix, thresholdParam, previous, current, i));
            }
        }
        // Last open-ended bucket
        int last = bounds[bounds.length - 1] * multiplier;
        sb.append(String.format("COUNT(%1$s_interval_min) FILTER (WHERE %1$s_interval_min > :%2$s + %3$d) as %1$s_b_%4$d \n",
                prefix, thresholdParam, last, bounds.length));

        return sb.toString();
    }

    public static String buildBaseColumns(String prefix, String thresholdParam) {
        return String.format("""
                COUNT(%1$s_interval_min) as %1$s_total,
                COUNT(%1$s_interval_min) FILTER (WHERE %1$s_interval_min <= :%2$s) as %1$s_met,
                COUNT(%1$s_interval_min) FILTER (WHERE %1$s_interval_min > :%2$s) as %1$s_breach,
                AVG(%1$s_interval_min) as %1$s_avg,
                percentile_cont(0.5) WITHIN GROUP (ORDER BY %1$s_interval_min) as %1$s_median,
                percentile_cont(0.9) WITHIN GROUP (ORDER BY %1$s_interval_min) as %1$s_p90,
                """, prefix, thresholdParam);
    }

    public static BreachDistributionDTO mapBreachDistribution(ResultSet rs, String prefix, int[] bounds, String unit) throws SQLException {
        long totalBreaches = 0;
        long[] counts = new long[bounds.length + 1];
        for (int i = 0; i <= bounds.length; i++) {
            counts[i] = rs.getLong(prefix + "_b_" + i);
            totalBreaches += counts[i];
        }

        BreachDistributionDTO.Metadata metadata = BreachDistributionDTO.Metadata.builder()
                .unit(unit)
                .totalCount(totalBreaches)
                .build();

        List<BreachDistributionDTO.Item> items = new ArrayList<>();
        int prev = 0;
        for (int i = 0; i < bounds.length; i++) {
            long count = counts[i];
            items.add(BreachDistributionDTO.Item.builder()
                    .sortOrder(i + 1)
                    .minBound(prev)
                    .maxBound(bounds[i])
                    .count(count)
                    .ratio(totalBreaches == 0 ? 0.0 : Math.round((double) count / totalBreaches * 1000.0) / 1000.0)
                    .build());
            prev = bounds[i];
        }

        items.add(BreachDistributionDTO.Item.builder()
                .sortOrder(bounds.length + 1)
                .minBound(prev)
                .maxBound(null)
                .count(counts[bounds.length])
                .ratio(totalBreaches == 0 ? 0.0 : Math.round((double) counts[bounds.length] / totalBreaches * 1000.0) / 1000.0)
                .build());

        return BreachDistributionDTO.builder()
                .metadata(metadata)
                .items(items)
                .build();
    }
}
