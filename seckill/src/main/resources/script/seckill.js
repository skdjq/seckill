var seckill = {
    //封装秒杀相关ajax的url
    URL: {
        now: function(){
            return '/seckill/time/now';
        },
        exposer: function(id){
            return '/seckill/' + id + '/exposer';
        },
        execution : function(id,md5){
            return '/seckill/' + id + '/' + md5 + '/execution';
        }
    },
    //处理秒杀逻辑
    handleSecKillKill: function(secKillId,node){
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');

        $.post(seckill.URL.exposer(secKillId),{},function(result){
            if(result && result.success){
                var exposer = result.data;

                if(exposer.exposed){
                    //开启秒杀
                    //获取秒杀地址
                    var killUrl =  seckill.URL.execution(secKillId,exposer.md5);
                    console.log('killUrl:',killUrl);
                    //绑定一次点击事件
                    $('#killBtn').one('click',function(){
                        //执行秒杀请求
                        $(this).addClass('disabled');
                        $.post(killUrl,{},function(result){
                            if(result && result.success){
                                var killResult = result.data;
                                var state = killResult.state;
                                var stateInfo = killResult.stateInfo;

                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });

                    node.show();
                }else{
                    //未开启秒杀
                    //重新计算计时逻辑
                    seckill.countdown(secKillId,exposer.now,exposer.start,exposer.end);
                }

            }else{
                console.error('result:',result);
            }
        });
    },
    //计时
    countdown: function(secKillId,nowTime,startTime,endTime){
        var $secKillBox = $('#seckill-box');

        if(nowTime > endTime){
            $secKillBox.html('秒杀结束');
        }else if(nowTime < startTime){
            $secKillBox.html('秒杀未开始');
            var killTime = new Date(startTime + 1000);

            $secKillBox.countdown(killTime,function(event){
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                $secKillBox.html(format);
            }).on('finish.countdown',function(){
                //获取秒杀地址，控制实现逻辑，执行秒杀
                seckill.handleSecKillKill(secKillId,$secKillBox);
            });
        }else{
            //秒杀开始
            seckill.handleSecKillKill(secKillId,$secKillBox);
        }


    },
    //验证手机号
    validatePhone: function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },
    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function(params){
            //用户手机验证和登录，计时交互
            //规划交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone'),
                startTime = params.startTime,
                endTime = params.endTime,
                secKillId = params.secKillId;

            //验证手机号
            if(!seckill.validatePhone(killPhone)){
                var killPhoneModal = $('#killPhoneModal');

                killPhoneModal.modal({
                    show: true,
                    backdrop: 'static',//禁止位置关闭
                    keyboard: false//关闭键盘事件
                });

                $('#killPhoneBtn').click(function(){
                    var inputPhone = $('#killPhoneKey').val();
                    if(seckill.validatePhone(inputPhone)){
                        //电话写入cookie
                        $.cookie('killPhone',inputPhone,{expires:7,path: '/seckill'})
                        window.location.reload();

                    }else{
                        //正常下会有一个前端字典
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号码错误</label>').show(300);
                    }
                });
            }

            //用户已经登录
            //计时交互
            $.get(seckill.URL.now(),function(result){
                if(result && result.success){
                    var nowTime = result.data;
                    seckill.countdown(secKillId,nowTime,startTime,endTime);

                }else{
                    consolw.error('result:',result);
                }
            });
        }

    }

}