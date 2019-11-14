package cn.huanletao.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.huanletao.common.pojo.Cart;
import com.huanletao.common.pojo.Product;
import com.huanletao.common.vo.SysResult;

import cn.huanletao.cart.mapper.CartMapper;

@Service
public class CartService {
	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private RestTemplate client;

	public List<Cart> queryMyCarts(String userId) {
		// TODO Auto-generated method stub
		return cartMapper.selectCartsByUserId(userId);
	}

	public void saveMycart(Cart cart) {
		// TODO Auto-generated method stub
		Cart exist = cartMapper.selectExistByUserIdAndProductId(cart);
		System.out.println(exist);
		if(exist != null) {
			//已存在，更新num数量
			//更新内存对象数据
			System.out.println("if");
			cart.setNum(cart.getNum()+exist.getNum());
			cartMapper.updateCartNumByUserIdAndProductId(cart);
			
		}else {
			//没有数量，新增购物车
			//获取商品服务返回的数据，封装补齐cart对象
			System.out.println("product:"+cart.getProductId());
			System.out.println("asdf:"+cart.getUserId());
			System.out.println("num:"+cart.getNum());
			Product prod = client.getForObject("http://productservice/product/manage/item/"+cart.getProductId(), Product.class);
			System.out.println("32432432::::"+prod);
			cart.setProductImage(prod.getProductImgurl());
			cart.setProductName(prod.getProductName());
			cart.setProductPrice(prod.getProductPrice());
			cart.setUserId(cart.getUserId());
			
			//调用Insert语句
			cartMapper.insertCart(cart);
		}
	}

	public void updateMyCartNum(Cart cart) {
		// TODO Auto-generated method stub
		cartMapper.updateCartNumByUserIdAndProductId(cart);
		
	}

	public void deleteMyCart(Cart cart) {
		// TODO Auto-generated method stub
		cartMapper.deleteCartByUserIdAndProductId(cart);
		
	}

	public void deleteAll() {
		// TODO Auto-generated method stub
		cartMapper.deleteAllCart();
	}

	

	

	

}
