package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SecKillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.SecKill;
import org.seckill.enums.SecKillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SecKillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SecKillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    /**
     * 列表
     * */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model){
        List<SecKill> list = seckillService.getSecKillList();
        model.addAttribute("list", list);
        return "list";
    }

    /**
     * 详情页
     * */
    @RequestMapping(value = "/{secKillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("secKillId") Long secKillId, Model model){
        if(secKillId == null){
            return "redirect:/seckill/list";
        }
        SecKill secKill = seckillService.getById(secKillId);
        if(secKill == null){
            return "forward:/seckill/list";
        }
        model.addAttribute("secKill", secKill);
        return "detail";
    }

    /**
     * 接口地址
     * */
    @RequestMapping(value = "/{secKillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("secKillId") Long secKillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSecKillUrl(secKillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch(Exception e){
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    /**
     * 执行秒杀
     * */
    @RequestMapping(value="/{secKillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=utf-8"})
    @ResponseBody
    public SeckillResult<SecKillExecution> excute(@PathVariable("secKillId") Long secKillId,
                                                  @PathVariable("md5") String md5,
                                                  @CookieValue(value="killPhone",required = false) Long userPhone){
        if(userPhone == null){
            return new SeckillResult<SecKillExecution>(false, "用户未注册！");
        }
        SeckillResult<SecKillExecution> result = null;
        try {
            SecKillExecution secKillExecution = seckillService.executeSecKill(secKillId,userPhone,md5);
            return new SeckillResult<SecKillExecution>(true,secKillExecution);

        } catch(RepeatKillException e){
            SecKillExecution secKillExecution = new SecKillExecution(secKillId, SecKillStatEnum.REPEAT);
            return new SeckillResult<SecKillExecution>(true,secKillExecution);

        }catch(SecKillCloseException e){
            SecKillExecution secKillExecution = new SecKillExecution(secKillId, SecKillStatEnum.END);
            return new SeckillResult<SecKillExecution>(true,secKillExecution);

        }catch(Exception e){
            logger.error(e.getMessage(),e);
            SecKillExecution secKillExecution = new SecKillExecution(secKillId, SecKillStatEnum.INNER_ERROR);
            return new SeckillResult<SecKillExecution>(true,secKillExecution);
        }
    }

    /**
     * 获取系统当前时间
     * */
    @RequestMapping(value="/time/now",method=RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }
}
