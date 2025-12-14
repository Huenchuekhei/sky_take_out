package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常(地址为空吗,购物车为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //地址为空
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //检查用户收货地址是否超出配送范围
        checkOutOfRange(addressBook.getCityCode()+addressBook.getCityName()+addressBook.getDetail());
        //获取线程中的用户id
        Long userid = BaseContext.getCurrentId();
        //创建新的实体类
        ShoppingCart shoppingCart = new ShoppingCart();
        //封装数据，给实体类赋值
        shoppingCart.setUserId(userid);
        //获取购物车数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断购物车数据是否为空
        if (list == null || list.isEmpty()) {
            //购物车为空
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向Orders表插入1条数据
        //将DTO类重新属性拷贝到对应实体类
        //查询用户的购物车数据(如果主键自增,则前端无法传递自动生成的主键,需要额外赋值)
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(1);
        orders.setUserId(userid);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPayStatus(0);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向orderDetail表插入n条数据
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车数据
        shoppingCartMapper.deleteByUserId(userid);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
/*
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
*/
        log.info("跳过微信支付，直接返回模拟数据");
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过webscoket 向客户端游览器推送消息 type orderId content
        Map map=new HashMap();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+outTradeNo);
        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult historyOrders(Integer page, Integer pageSize, Integer status) {
        /*分页查询历史订单*/
        PageHelper.startPage(page, pageSize);
        /*可以根据订单状态查询*/
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> ordersPage = orderMapper.pageQuery(ordersPageQueryDTO);
        /*展示订单数据时，需要展示的数据包括：下单时间、订单状态、订单金额、订单明细（商品名称、图片）*/
        List<OrderVO> list = new ArrayList<>();
        if (ordersPage != null && ordersPage.getTotal() > 0) {
            for (Orders orders : ordersPage) {
                Long id = orders.getId();
                //查询订单明细数据,根据订单id查询对应的订单明细数据
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
                //封装订单数据和订单明细数据到订单vo
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return new PageResult(ordersPage.getTotal(), list);
    }

    @Override
    public OrderVO orderDetails(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        /*待支付和待接单状态下，用户可直接取消订单*/
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        /*商家已接单状态下，用户取消订单需电话沟通商家*/
        if (orders.getStatus() >= 2) {
            throw new RuntimeException("商家已接单，不能取消订单，请电话联系商家");
        }
        /*取消订单后需要将订单状态修改为“已取消”*/
        Orders updateOrders = new Orders();
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED) && orders.getPayStatus().equals(Orders.PAID)) {
            updateOrders = Orders.builder()
                    .id(id)
                    .userId(orders.getUserId())
                    .status(Orders.CANCELLED)
                    .cancelReason("用户取消")
                    .payStatus(Orders.REFUND)
                    .cancelTime(LocalDateTime.now())
                    .build();
            orderMapper.update(updateOrders);
        }
    }

    @Override
    public void repeatOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new RuntimeException("订单明细信息有误");
        }
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersPage = orderMapper.pageQuery(ordersPageQueryDTO);
        if (ordersPage == null || ordersPage.getTotal() == 0) {
            throw new RuntimeException("未查询到订单信息");
        }
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : ordersPage) {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            if (orderDetailList == null || orderDetailList.isEmpty()) {
                throw new RuntimeException("订单明细信息有误");
            }
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            orderVOList.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        return null;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        // 商家接单其实就是将订单的状态修改为“已接单”
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        Orders updateOrders = Orders.builder()
                .id(orders.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(updateOrders);
    }

    @Override
    public void cancel(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            Orders updateOrders = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason(ordersRejectionDTO.getRejectionReason())
                    .cancelTime(LocalDateTime.now())
                    .build();
            orderMapper.update(updateOrders);
        }
    }

    @Override
    public void deliver(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        Orders updateOrders = Orders.builder()
                .id(orders.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(updateOrders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单信息有误");
        }
        if (orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            Orders updateOrders = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.COMPLETED)
                    .build();
            orderMapper.update(updateOrders);
        }
    }

    @Override
    public void remind(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        log.info("用户催单，订单id：{}", id);
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orders.getNumber());
        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 50000000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
