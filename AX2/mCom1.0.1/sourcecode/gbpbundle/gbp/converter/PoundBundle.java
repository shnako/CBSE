package gbp.converter;

import mcom.bundle.ContractType;
import mcom.bundle.annotations.mController;
import mcom.bundle.annotations.mControllerInit;
import mcom.bundle.annotations.mEntity;
import mcom.bundle.annotations.mEntityContract;

import com.tunyk.currencyconverter.BankUaCom;
import com.tunyk.currencyconverter.api.Currency;
import com.tunyk.currencyconverter.api.CurrencyConverter;
import com.tunyk.currencyconverter.api.CurrencyConverterException;

@mController
@mEntity
public class PoundBundle {
	//test1:returns the current value in pound for any supported currency.	 
	static final String supportedCurrencies  = "CURRENCY: UAH,AUD,AZM,GBP,BYR,DKK,USD,EUR,NOK,CHF,CNY,JPY";
	@mEntityContract(description=supportedCurrencies, contractType = ContractType.GET)	
	@mControllerInit
	public static Float convertPound(String c_type, double amount, int it){
		
		try {
			CurrencyConverter currencyConverter = new BankUaCom(Currency.GBP, Currency.EUR);
			Float value = currencyConverter.convertCurrency(1f, Currency.EUR, Currency.valueOf(c_type));
			return value;
		} 
		catch (CurrencyConverterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
