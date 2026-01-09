package com.example.bankcards.persistence;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.YearMonth;

public class CardSpecifications {

    public static Specification<Card> belongsTo(UserInfo user) {
        return (root, query, cb) ->
            cb.equal(root.get("owner"), user);
    }

    public static Specification<Card> hasStatus(Status status) {
        return (root, query, cb) ->
            cb.equal(root.get("status"), status);
    }

    public static Specification<Card> expiryBefore(YearMonth date) {
        return (root, query, cb) ->
            cb.lessThan(root.get("expiryDate"), date);
    }

    public static Specification<Card> expiryAfter(YearMonth date) {
        return (root, query, cb) ->
            cb.greaterThan(root.get("expiryDate"), date);
    }

    public static Specification<Card> balanceMin(BigDecimal min) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("balance"), min);
    }

    public static Specification<Card> balanceMax(BigDecimal max) {
        return (root, query, cb) ->
            cb.lessThanOrEqualTo(root.get("balance"), max);
    }
}
