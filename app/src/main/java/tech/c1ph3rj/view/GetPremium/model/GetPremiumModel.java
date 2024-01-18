package tech.c1ph3rj.view.GetPremium.model;

import java.util.Objects;

public class GetPremiumModel {
    public boolean isSelected;
    String productName, basicPremium, addonsPremium, discountPremium, beforeTax, taxPremium, totalPremium;

    public GetPremiumModel(String productName, String basicPremium, String addonsPremium, String discountPremium, String beforeTax, String taxPremium, String totalPremium) {
        this.productName = productName;
        this.basicPremium = basicPremium;
        this.addonsPremium = addonsPremium;
        this.discountPremium = discountPremium;
        this.beforeTax = beforeTax;
        this.taxPremium = taxPremium;
        this.totalPremium = totalPremium;
        isSelected = false;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBasicPremium() {
        return basicPremium;
    }

    public void setBasicPremium(String basicPremium) {
        this.basicPremium = basicPremium;
    }

    public String getAddonsPremium() {
        return addonsPremium;
    }

    public void setAddonsPremium(String addonsPremium) {
        this.addonsPremium = addonsPremium;
    }

    public String getDiscountPremium() {
        return discountPremium;
    }

    public void setDiscountPremium(String discountPremium) {
        this.discountPremium = discountPremium;
    }

    public String getBeforeTax() {
        return beforeTax;
    }

    public void setBeforeTax(String beforeTax) {
        this.beforeTax = beforeTax;
    }

    public String getTaxPremium() {
        return taxPremium;
    }

    public void setTaxPremium(String taxPremium) {
        this.taxPremium = taxPremium;
    }

    public String getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(String totalPremium) {
        this.totalPremium = totalPremium;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        GetPremiumModel that = (GetPremiumModel) obj;

        if (isSelected != that.isSelected) return false;
        if (!Objects.equals(productName, that.productName)) return false;
        if (!Objects.equals(basicPremium, that.basicPremium)) return false;
        if (!Objects.equals(addonsPremium, that.addonsPremium))
            return false;
        if (!Objects.equals(discountPremium, that.discountPremium))
            return false;
        if (!Objects.equals(beforeTax, that.beforeTax)) return false;
        if (!Objects.equals(taxPremium, that.taxPremium)) return false;
        return Objects.equals(totalPremium, that.totalPremium);
    }

    // You should also override hashCode when overriding equals
    @Override
    public int hashCode() {
        int result = productName != null ? productName.hashCode() : 0;
        result = 31 * result + (basicPremium != null ? basicPremium.hashCode() : 0);
        result = 31 * result + (addonsPremium != null ? addonsPremium.hashCode() : 0);
        result = 31 * result + (discountPremium != null ? discountPremium.hashCode() : 0);
        result = 31 * result + (beforeTax != null ? beforeTax.hashCode() : 0);
        result = 31 * result + (taxPremium != null ? taxPremium.hashCode() : 0);
        result = 31 * result + (totalPremium != null ? totalPremium.hashCode() : 0);
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }
}
