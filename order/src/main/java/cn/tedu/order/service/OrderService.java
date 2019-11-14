package cn.tedu.order.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.huanletao.common.pojo.Order;
import com.huanletao.common.pojo.OrderItem;
import com.huanletao.common.pojo.Product;
import com.huanletao.common.vo.SysResult;

import cn.tedu.order.mapper.OrderMapper;

@Service
public class OrderService {
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private RestTemplate client;
	public List<Order> queryMyOrders(String userId) {
		return orderMapper.selectOrdersByUserId(userId);
	}
	public void saveOrder(Order order) {
		//补齐数据 orderId orderTime orderPaystate
		//0表示未支付,1表示支付
		
		order.setOrderId(UUID.randomUUID().toString());
		order.setOrderTime(new Date());
		order.setOrderPaystate(0);
		orderMapper.insertOrder(order);
		
	}
	public void deleteOrder(String orderId) {
		orderMapper.deleteOrderByOrderId(orderId);
		
	}
	public void updatePaystate(String orderId) throws Exception {
		// TODO Auto-generated method stub
		List<OrderItem> oi = orderMapper.queryItemsById(orderId);
		SysResult result=null;
		System.out.println("----------------------------");
		System.out.println(oi.toString());
		for (OrderItem orderItem : oi) {
			System.out.println("----------------------------");
			System.out.println(orderItem);
			System.out.println("----------------------------");
			String productId = orderItem.getProductId();
			Integer num = orderItem.getNum();
			 result = client.getForObject("http://productservice/product/manage/updateSales?productId="+productId+"&num="+num,SysResult.class );
		}
		if (result.getStatus()!=200) {
			throw new Exception(result.getMsg());
		}
		orderMapper.updatePaystate(orderId);
		
	}
	public List<Order> queryMyOrders1(String userId) {
		// TODO Auto-generated method stub
		return orderMapper.selectOrdersByUserId1(userId);
	}

}
