package shn.shopify.logic;

import wbs.framework.database.Transaction;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequestItem;
import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

import shn.shopify.model.ShnShopifyConnectionRec;
import shn.shopify.model.ShnShopifyRecord;

public
interface ShnShopifySynchronisation <
	Self extends ShnShopifySynchronisation <Self, Local, Request, Response>,
	Local extends ShnShopifyRecord <Local>,
	Request extends ShopifyApiRequestItem,
	Response extends ShopifyApiResponseItem
> {

	String friendlyNameSingular ();
	String friendlyNamePlural ();

	Self enableCreate (
			Boolean enableCreate);

	Self enableUpdate (
			Boolean enableUpdate);

	Self enableRemove (
			Boolean enableRemove);

	Self maxOperations (
			Long maxOperations);

	Self shopifyConnection (
			ShnShopifyConnectionRec shopifyConnection);

	Self shopifyCredentials (
			ShopifyApiClientCredentials shopifyCredentials);

	Self synchronise (
			Transaction parentTransaction);

	long numCreated ();
	long numUpdated ();
	long numRemoved ();

	long numNotCreated ();
	long numNotUpdated ();
	long numNotRemoved ();

	long numDataErrors ();
	long numEncodeErrors ();
	long numMismatchErrors ();
	long numApiErrors ();

	long numOperations ();

}
