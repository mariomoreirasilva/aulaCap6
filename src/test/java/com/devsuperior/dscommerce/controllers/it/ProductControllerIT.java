package com.devsuperior.dscommerce.controllers.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class ProductControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private String productName;
	private String clientUserName, clientPassword, adminUserName, adminPassword;
	private String clientToken, invalidToken, adminToken;
	
	private ProductDTO productDTO;
	private Product product;
	
	@BeforeEach
	 void setUp() throws Exception {
		
		clientUserName = "maria@gamil.com";
		clientPassword = "123456";
		adminUserName  = "alex@gmail.com";
		adminPassword  = "123456";
		
		productName = "Macbook";
		
		adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUserName, adminPassword);
		clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUserName, clientPassword);
		invalidToken = adminToken + "xpto"; //simular token inválido
		
		//podia usar a factory ja criada. naõ sei pq o professor fez na mão
		product = new Product(null, "Teste PlayStation5", "Lorem ipsum, dolor sit amet consectetur", 3999.90, "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		Category category = new Category(2L, "Eletro");
		product.getCategories().add(category);
		productDTO = new ProductDTO(product);
	}
	@Test
	public void findAllShouldReturnPageWhenProductNameParamIsEmpty() throws Exception {
		
		ResultActions result = 
				mockMvc.perform(get("/products")
					.accept(MediaType.APPLICATION_JSON))
					.andDo(MockMvcResultHandlers.print());
		
		result.andExpect(status().isOk());	
		result.andExpect(jsonPath("$.content[0].id").value(1L));
		result.andExpect(jsonPath("$.content[0].name").value("The Lord of the Rings"));
		result.andExpect(jsonPath("$.content[0].price").value(90.5));
		result.andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"));
	}
	
	
	@Test
	public void findAllShouldReturnPageWhenProductNameParamIsNotEmpty() throws Exception {
		
		ResultActions result = 
				mockMvc.perform(get("/products?name={productName}", productName)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content[0].id").value(3L));
		result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
		result.andExpect(jsonPath("$.content[0].price").value(1250.0));
		result.andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
	}
	
	@Test
	public void insertShouldReturnProductDTOCreatedWhenAdminLogged() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 
				mockMvc.perform(post("/products")
					.header("Authorization", "Bearer " + adminToken)	
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andDo(MockMvcResultHandlers.print()); //chamada pra debug
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").value(26L)); //tenho 25 produtos, então este é o 26
		result.andExpect(jsonPath("$.name").value("Teste PlayStation5"));
		result.andExpect(jsonPath("$.description").value("Lorem ipsum, dolor sit amet consectetur"));
		result.andExpect(jsonPath("$.price").value( 3999.90));
		result.andExpect(jsonPath("$.imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"));
		result.andExpect(jsonPath("$.categories[0].id").value(2L));
		
	}

}
