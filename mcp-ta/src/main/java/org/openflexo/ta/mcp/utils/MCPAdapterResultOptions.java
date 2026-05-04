package org.openflexo.ta.mcp.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MCPAdapterResultOptions {

    public enum TargetType {
        CSV_DOCUMENT,
        CSV_ROW
    }

    private final TargetType targetType;
    private final List<String> columnNames;
    private final String delimiter;
    private final boolean hasHeader;

    private MCPAdapterResultOptions(Builder b) {
        this.targetType  = b.targetType;
        this.columnNames = b.columnNames;
        this.delimiter   = b.delimiter;
        this.hasHeader   = b.hasHeader;
    }

    public TargetType getTargetType()    { return targetType; }
    public List<String> getColumnNames() { return columnNames; }
    public String getDelimiter()         { return delimiter; }
    public boolean isHasHeader()         { return hasHeader; }


    public static MCPAdapterResultOptions forCSVDocument() {
        return new Builder(TargetType.CSV_DOCUMENT).build();
    }

    public static MCPAdapterResultOptions forCSVRow(String... columnNames) {
        return new Builder(TargetType.CSV_ROW)
                .withColumnNames(Arrays.asList(columnNames))
                .build();
    }


    public static final class Builder {
        private final TargetType targetType;
        private List<String> columnNames = Collections.emptyList();
        private String delimiter = ",";
        private boolean hasHeader = true;

        public Builder(TargetType targetType) {
            this.targetType = targetType;
        }

        public Builder withColumnNames(List<String> cols) {
            this.columnNames = cols; return this;
        }
        public Builder withDelimiter(String d) {
            this.delimiter = d; return this;
        }
        public Builder withHeader(boolean h) {
            this.hasHeader = h; return this;
        }
        public MCPAdapterResultOptions build() {
            return new MCPAdapterResultOptions(this);
        }
    }
}