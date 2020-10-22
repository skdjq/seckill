<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@include file="common/tag.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <%@include file="common/head.jsp" %>
    <link rel="stylesheet" href="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css">
    <style>.modal-backdrop{z-index:0;}</style>
    <title>秒杀详情页</title>
</head>
<body>
<div class="container">
    <div class="panel panel-default text-center">
        <div class="pannel-heading">
            <h1>${secKill.name}</h1>
        </div>

        <div class="panel-body">
            <h2 class="text-danger">
                <%--显示time图标--%>
                <span class="glyphicon glyphicon-time"></span>
                <%--展示倒计时--%>
                <span class="countdown" id="seckill-box"></span>
            </h2>
        </div>
    </div>
</div>

<div id="killPhoneModal" class="modal fade">

    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title text-center">
                    <span class="glyphicon glyphicon-phone"></span>秒杀电话：
                </h3>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-8 col-xs-offset-2">
                        <input type="text" name="killPhone" id="killPhoneKey"
                               placeholder="请输入手机号" class="form-control" />
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <!-- 验证信息 -->
                <span id="killPhoneMessage" class="glyhicon"></span>
                <button type="button" id="killPhoneBtn" class="btn btn-success">
                    <span class="glyphicon glyphicon-phone"></span>
                    Submit
                </button>
            </div>

        </div>
    </div>
</div>




<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>

<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>

<%--jQuery Cookie操作插件--%>
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<%--jQuery countDown倒计时插件--%>
<script src="http://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>

<%--<script src="${basePath}resources/script/seckill.js" type="text/javascript"></script>--%>
<script>
    var seckill = {
        //封装秒杀相关ajax的url
        URL: {
            now: function(){
                return '/seckill/time/now';
            },
            exposer: function(secKillId){
                return '/seckill/' + secKillId + '/exposer';
            },
            execution : function(secKillId,md5){
                return '/seckill/' + secKillId + '/' + md5 + '/execution';
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
                            $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号码错误</label>').show(500);
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
</script>
<script type="text/javascript">
    $(function(){
        //使用EL表达式传入参数
        seckill.detail.init({
            secKillId: ${secKill.secKillId},
            startTime: ${secKill.startTime.time},
            endTime: ${secKill.endTime.time}
        });
    });
</script>





</body>
</html>