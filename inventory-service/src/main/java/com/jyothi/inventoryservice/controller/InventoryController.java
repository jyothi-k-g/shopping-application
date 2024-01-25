package com.jyothi.inventoryservice.controller;

import com.jyothi.inventoryservice.dto.InventoryResponse;
import com.jyothi.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;
    //if user is placing many order
    //http://localhost:8082/api/inventory/iPhone-13,iphone13-red first format appending all products with comma
    // http://localhost:8082/api/inventroy?sku-code=iphone_13&skucode=iPhone_red second format
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInstock(@RequestParam  List<String> skuCode){
        return inventoryService.isInStock(skuCode);

    }
}
