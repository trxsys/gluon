package test.validation.Store;

import java.util.List;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "hasOrders treatOrder;")
class WorkerAux
{
    public static boolean hasOrders()
    {
        return Store.hasOrders();
    }

	@Atomic
    public static String treatOrder()
    {
		Order order = Store.getOrder();
		int total = 0;
		List<Pair<Product,Integer>> list = order.getList();
		while(list.size() > 0){
			Pair<Product, Integer> pair = list.remove(0);
			Product p = pair.getFirst();
			int n = pair.getSecond();
			total += Store.getPrice(p);
			Store.sell(p,n);
		}
		return "Sell{"+42+" Client = "+order.getClient()+" | Nº prod = "+order.size()+"\n\n";
    }
}
