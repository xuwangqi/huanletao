package cn.huanletao.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.huanletao.service.ProductService;

@RestController
public class IndexCreateController {
	@Autowired
	private ProductService productService;
	//localhost:10003/create/index
	@RequestMapping("create/index")
	public String createIndex(){
		//通过productService查询所有商品数据
		try{
			productService.creatIndex();
			return "success";
		}catch(Exception e){
			return "faild";
		}
	}
	

}



