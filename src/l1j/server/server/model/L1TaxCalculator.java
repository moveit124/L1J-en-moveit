/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.model;

import l1j.server.Config;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.shop.L1ShopBuyOrder;
import l1j.server.server.model.shop.L1ShopBuyOrderList;
import l1j.server.server.templates.L1ShopItem;

public class L1TaxCalculator {
	// Static tax constants
	private static final int WAR_TAX_RATES = 15;        // War tax % for Diad control
	private static final int NATIONAL_TAX_RATES = 10;   // 10% of castle tax to Aden
	private static final int DIAD_TAX_RATES = 10;       // 10% of castle tax to Diad

	private final int _taxRatesCastle;
	private final int _taxRatesTown;
	private final int _taxRatesWar = WAR_TAX_RATES;

	public L1TaxCalculator(int merchantNpcId) {
		_taxRatesCastle = CastleTable.getInstance().getCastleTable(5).getTaxRate(); // Heine only
		_taxRatesTown = L1TownLocation.getTownTaxRateByNpcid(merchantNpcId);
	}

	// Calculates total tax applied to item purchase (used at final price calculation)
	public int calcTotalTaxPrice(L1ShopItem item, int count) {
		int taxCastle = calcCastleTaxPrice(item, count, false); // false = don't subtract diad/aden
		int taxTown = calcTownTaxPrice(item, count);
		return taxCastle + taxTown;
	}

	// Overload for full order list
	public int calcCastleTaxPrice(L1ShopBuyOrderList orderList) {
		return calcCastleTaxPrice(orderList, true); // true = subtract diad/aden cut
	}

	public int calcCastleTaxPrice(L1ShopBuyOrderList orderList, boolean subtractOtherTaxes) {
		int totalTax = 0;
		for (L1ShopBuyOrder item : orderList.getBoughtItems()) {
			totalTax += calcCastleTaxPrice(item.getItem(), item.getCount(), subtractOtherTaxes);
		}
		return totalTax;
	}

	public int calcCastleTaxPrice(L1ShopItem item, int count) {
		return calcCastleTaxPrice(item, count, true);
	}

	// Main castle tax calculation (per-item)
	public int calcCastleTaxPrice(L1ShopItem item, int count, boolean subtractOtherTaxes) {
		int fullPrice = (int)(item.getPrice() * count * Config.RATE_SHOP_SELLING_PRICE);
		int castleTax = fullPrice * _taxRatesCastle / 100;

		if (!subtractOtherTaxes) {
			return castleTax;
		}

		// Subtract Diad and National tax shares from castle's portion
		int nationalTax = castleTax * NATIONAL_TAX_RATES / 100;
		int diadTax = castleTax * DIAD_TAX_RATES / 100;

		return castleTax - nationalTax - diadTax;
	}

	public int calcNationalTaxPrice(L1ShopBuyOrderList orderList) {
		int totalTax = 0;
		for (L1ShopBuyOrder item : orderList.getBoughtItems()) {
			totalTax += calcNationalTaxPrice(item.getItem(), item.getCount());
		}
		return totalTax;
	}

	public int calcNationalTaxPrice(L1ShopItem item, int count) {
		int fullPrice = (int)(item.getPrice() * count * Config.RATE_SHOP_SELLING_PRICE);
		int castleTax = fullPrice * _taxRatesCastle / 100;
		return castleTax * NATIONAL_TAX_RATES / 100;
	}

	public int calcTownTaxPrice(L1ShopItem item, int count) {
		int fullPrice = (int)(item.getPrice() * count * Config.RATE_SHOP_SELLING_PRICE);
		return fullPrice * _taxRatesTown / 100;
	}

	public int calcWarTaxPrice(L1ShopItem item, int count) {
		int fullPrice = (int)(item.getPrice() * count * Config.RATE_SHOP_SELLING_PRICE);
		return fullPrice * _taxRatesWar / 100;
	}

	public int calcDiadTaxPrice(L1ShopBuyOrderList orderList) {
		int totalTax = 0;
		for (L1ShopBuyOrder item : orderList.getBoughtItems()) {
			totalTax += calcDiadTaxPrice(item.getItem(), item.getCount());
		}
		return totalTax;
	}

	public int calcDiadTaxPrice(L1ShopItem item, int count) {
		int fullPrice = (int)(item.getPrice() * count * Config.RATE_SHOP_SELLING_PRICE);
		int castleTax = fullPrice * _taxRatesCastle / 100;
		return castleTax * DIAD_TAX_RATES / 100;
	}

	public int layTax(L1ShopItem item, int count) {
		return calcTotalTaxPrice(item, count);
	}
}

