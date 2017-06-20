package shn.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

@Accessors (fluent = true)
@Data
public
class ShopifyProductCreateResponse {

	@DataChild (
		name = "product")
	ShopifyProductResponse product;

}
