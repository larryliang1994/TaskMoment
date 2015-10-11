//登录页面

function loginSubmit() {
    var telephone = $("input[name=telephone]").val();
    var verify = $("input[name=verify]").val();
    window.login.onSubmit(telephone, verify);
}

function verify() {
    var telephone = $("input[name=telephone]").val();
    window.login.onGetVerify(telephone);
}

function onManage() {

  window.function.member_manage();

}

function addCompanySubmit() {

    window.addcompany.onAddCompany();
}

function addMember(){
	window.member.onAddMember();
}

/**
 * 添加新成员 chengyuan_add.php
 */
function member_add(o){
    if($(o).attr("stop")==="saving"){ return false; } // 1-5 避免重复点击
    $(o).attr("stop","saving");
    $(o).attr("temp_info",$(o).html()); // 2-5 将按钮上的文字记录下来
    $(o).html('提交中'); //3-5 重置按钮上的文字
    
    window.addmember.onSubmit($('input[name=mobile]').val(), 'setInnerHtml', "[stop=saving]");

    return false;
}

function setInnerHtml(o, value) 
{
	$(o).html(value);
}

/**
 * 删除成员 chengyuan.php
 */
function delChengyuan(o){
    var data = {
        id : $(o).attr('chenyuan_id'),
        mid : $(o).attr('mid'),
    };

    window.member.onDeleteMember($(o).attr('chenyuan_id'), $(o).attr('mid'));

    return false;
}

/**
 * 成员列表
 */
function getMember(info){
    var shops = info;
    
    $('#_loading_tr1').css('display','none');
    if(shops){
        console.log(shops);
        var i = null;
        for(i in shops){
            var id   = shops[i]['id'];
            var mid   = shops[i]['mid'];
            var real_name = shops[i]['real_name'];
            var mobile = shops[i]['mobile'];
            if (real_name) {
                name = real_name + ' ' + mobile;
            }else{
                name  = mobile;
            }
            $shop = $('#_clone_tr1').clone().removeAttr('id').css('display','');
            $shop.children('td').first().html(name);
            $shop.find('a').attr({
                'chenyuan_id': id,
                'mid': mid
            }).css('display','');
            $('#_clone_tr1').before($shop);
        }
    }else{
        $('#_clone_tr1').css('display','');
    }
}


/* create_company.php */

function CreateCompany(o){
	if($(o).attr("stop")==="saving"){ return false; } // 1-5 避免重复点击
	$(o).attr("stop","saving");
	$(o).attr("temp_info",$(o).html()); // 2-5 将按钮上的文字记录下来
	$(o).html('提交中'); //3-5 重置按钮上的文字

    window.addcompany.onSubmit($('input[name=name]').val());

	return false;
}

/**
 * 我创建的公司列表
 */
function getMyCompany(info){
    var shops = info;

    	$('#_loading_tr1').css('display','none');
        if(shops){
            var len = shops.length;

            for(var i=0;i<len;i++){
                $shop = $('#_clone_tr1').clone().removeAttr('id').css('display','');
                $shop.find('._name').text(shops[i]['name']).attr("cid", shops[i]['cid']).click(function () {
                	//var n = $shop.find('._name').html(); //公司名称

                	window.company.onFunction($(this).html(), $(this).attr("cid"));
                });
                $('#_clone_tr1').before($shop);
            }
        }else{
        	$('#_clone_tr1').css('display','');
        }
}

/**
 * 我加入的公司列表
 */
function getMyJoinCompany(info){
    var shops = info;

	$('#_loading_tr2').css('display','none');
    if(shops){
    	$('#_clone_tr2').css('display','none');
        var len = shops.length;
        for(var i=0;i<len;i++){
//            $shop = $('#_clone_tr2').clone().removeAttr('id').css('display','');
//            $shop.find('._name').text(shops[i]['name']);
//            $shop.find('._name').attr('href','renwuquan.php?share_active_cid='+shops[i]['id']+'&name='+shops[i]['name']);
//            $('#_clone_tr2').before($shop);
//
            $shop = $('#_clone_tr2').clone().removeAttr('id').css('display','');
            $shop.find('._name').text(shops[i]['name']).attr("cid", shops[i]['cid']).click(function () {
                //var n = $shop.find('._name').html(); //公司名称

                window.company.onFunction($(this).html(), $(this).attr("cid"));
            });
            $('#_clone_tr2').before($shop);
        }
    }else{
    	$('#_clone_tr2').css('display','');
    }
}

/**
 * 手机格式校验
 * @param  {[type]}  mobile [description]
 * @return {Boolean}        [description]
 */
	function isMobile (mobile){
		return (/^(13|15|18|14|17)[0-9]{9}$/.test(mobile));
	}


