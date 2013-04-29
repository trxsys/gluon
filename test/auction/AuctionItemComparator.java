package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

import java.util.Comparator;

/* AuctionItem comparator, see compare() comment. */
public class AuctionItemComparator implements Comparator<AuctionItem>
{
	public AuctionItemComparator()
	{

	}

	/* Returns < 0 if a1 < a2, 0 if a1 = a2, or > 0 if a1 > 0 according to
	 * what is needed in Main.InterpreterReportAuction().
	 */
	public int compare(AuctionItem a1, AuctionItem a2)
	{
		int r;

		if (a1.sold() && !a2.sold())
			r=-1;
		else if (!a1.sold() && a2.sold())
			r=1;
		else if (a1.sold()) /* From here on items are both sold
				       or both !sold */
        {
            Main.m.a();
			r=a1.getSeller().compareTo(a2.getSeller());
        }
		else
			r=a1.getName().compareTo(a2.getName());

        Main.m.b();

		return r;
	}
}
