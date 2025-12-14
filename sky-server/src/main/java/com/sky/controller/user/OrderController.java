package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
@Component("userOrderController")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("ordersSubmitDTO:{}", ordersSubmitDTO);
        return Result.success(orderService.submit(ordersSubmitDTO));
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单")
    public Result<PageResult> historyOrders(@RequestParam("page") Integer page,
                                           @RequestParam("pageSize") Integer pageSize,
                                           @RequestParam("status") Integer status) {
        log.info("历史订单：page={}, pageSize={}, status={}", page, pageSize, status);
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> orderDetails(@PathVariable("id") Long id) {
        log.info("订单详情：id={}", id);
        OrderVO orderVO = orderService.orderDetails(id);
        log.info("{}", orderVO);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result<Void> cancelOrder(@PathVariable("id") Long id) {
        log.info("取消订单：id={}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result<Void> repeatOrder(@PathVariable("id") Long id) {
        log.info("再来一单：id={}", id);
        orderService.repeatOrder(id);
        return Result.success();
    }
    @GetMapping("/remind/{id}")
    @ApiOperation("订单催单")
    public Result<Void> remind(@PathVariable("id") Long id) {
        log.info("订单催单：id={}", id);
        orderService.remind(id);
        return Result.success();
    }
}