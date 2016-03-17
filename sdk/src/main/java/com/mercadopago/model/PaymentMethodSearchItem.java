package com.mercadopago.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mreverter on 15/1/16.
 */
public class PaymentMethodSearchItem implements Serializable {

    private String id;
    private String type;
    private String description;
    private String comment;
    private List<PaymentMethodSearchItem> children;
    private String childrenHeader;
    private String itemToken;
    private Boolean securityCodeRequired;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<PaymentMethodSearchItem> getChildren() {
        return children;
    }

    public void setChildren(List<PaymentMethodSearchItem> children) {
        this.children = children;
    }

    public String getChildrenHeader() {
        return this.childrenHeader;
    }

    public boolean hasChildren() {
        return children != null && children.size() != 0;
    }

    public boolean isSecurityCodeRequired() {
        return securityCodeRequired != null && securityCodeRequired;
    }

    public String getItemToken() {
        return itemToken;
    }
}
