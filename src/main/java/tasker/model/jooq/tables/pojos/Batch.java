/*
 * This file is generated by jOOQ.
*/
package tasker.model.jooq.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.annotation.Generated;

import tasker.model.jooq.enums.BatchType;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Batch implements Serializable {

    private static final long serialVersionUID = 1651611529;

    private final UUID           id;
    private final BatchType      type;
    private final Integer        remain;
    private final Integer        total;
    private final OffsetDateTime created;
    private final OffsetDateTime modified;

    public Batch(Batch value) {
        this.id = value.id;
        this.type = value.type;
        this.remain = value.remain;
        this.total = value.total;
        this.created = value.created;
        this.modified = value.modified;
    }

    public Batch(
        UUID           id,
        BatchType      type,
        Integer        remain,
        Integer        total,
        OffsetDateTime created,
        OffsetDateTime modified
    ) {
        this.id = id;
        this.type = type;
        this.remain = remain;
        this.total = total;
        this.created = created;
        this.modified = modified;
    }

    public UUID getId() {
        return this.id;
    }

    public BatchType getType() {
        return this.type;
    }

    public Integer getRemain() {
        return this.remain;
    }

    public Integer getTotal() {
        return this.total;
    }

    public OffsetDateTime getCreated() {
        return this.created;
    }

    public OffsetDateTime getModified() {
        return this.modified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Batch (");

        sb.append(id);
        sb.append(", ").append(type);
        sb.append(", ").append(remain);
        sb.append(", ").append(total);
        sb.append(", ").append(created);
        sb.append(", ").append(modified);

        sb.append(")");
        return sb.toString();
    }
}
