package com.sky.controller.admin;


import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "商家订单相关接口")
@Slf4j
@Component("adminOrderController")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单条件查询")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单条件查询: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("'/statistics'")
    @ApiOperation("订单统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各状态订单统计");
        OrderStatisticsVO statisticsVO = orderService.statistics();
        return Result.success(statisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> details(@RequestParam("id") Long id) {
        log.info("订单详情: {}", id);
        OrderVO orderVO = orderService.orderDetails(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("确认订单")
    public Result<Void> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("确认订单: {}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("取消订单: {}", ordersRejectionDTO);
        orderService.cancel(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/deliver/{id}")
    @ApiOperation("派送订单")
    public Result<Void> deliver(@PathVariable("id") Long id) {
        orderService.deliver(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<Void> complete(@PathVariable("id") Long id) {
        log.info("完成订单: {}", id);
        orderService.complete(id);
        return Result.success();
    }
}