<template>
  <span>
    <el-button :style="myStyle" type="primary" size="medium" @click="show" >客户导入</el-button>
    <el-dialog title="客户导入" :visible.sync="customerDialogVisible" top="6vh" width="780px"
               :before-close="close"  destroy-on-close custom-class="role-mask" :close-on-click-modal="false">
      <div style="padding: 0px 10px">
        <div style="display: flex">
          <el-upload
              ref="upload"
              action=""
              :show-file-list="false"
              :accept="accept"
              :auto-upload="true"
              :before-upload="uploadCheck"
              :limit="1"
              :http-request="importTable">
            <el-button type="success" size="medium" :loading="importLoading">上传文件</el-button>
          </el-upload>
          <el-button style="margin-left: 20px" size="medium" @click="downloadTemple">下载模板</el-button>
        </div>
        <div style="margin: 15px 0px 0px 10px">
          <span>
            <span>导入状态：</span>
            <el-tag v-if="progressStatus===0" type="info">未开始</el-tag>
            <el-tag v-if="progressStatus===1">进行中</el-tag>
            <el-tag v-if="progressStatus===2" type="success">已完成</el-tag>
            <el-tag v-if="progressStatus===3" type="danger">错误</el-tag>
          </span>
        </div>
        <div style="margin: 15px 10px">
          <el-progress :percentage="progressNum"></el-progress>
        </div>
        <div style="margin-top: 20px">
          <el-table
              height="41vh"
              v-if="errorTableShow"
              :data="errorTableData"
              border stripe>
            <el-table-column
                width="120"
                type="index"
                label="序号">
          </el-table-column>
          <el-table-column
              width="120"
              :formatter="(row) => {return '第'+row.rowIndex+'行'}"
              label="表格行号">
          </el-table-column>
          <el-table-column
              :formatter="(row) => {return row.errorContent}"
              label="错误">
          </el-table-column>
        </el-table>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="close">关 闭</el-button>
      </div>
    </el-dialog>
  </span>
</template>

<script>
import { cookieUser } from "@/components/common/partten.js";
import {httpGet, httpPost, httpJson} from "@/api";
import departDialog from "@/components/page/drawer/departDialog.vue";
import bus from "@/assembly/bus";
import Vue from "vue";
import CustomerSelect from "@/components/page/components/customer/CustomerSelect.vue";
export default {
  props:{
    myStyle:{},
  },
  data() {
    return {
      importLoading:false,
      accept: '.xls,.xlsx',
      customerDialogVisible:false,
      errorTableShow:false,
      errorTableData:[],

      progressKey:null,
      progressNum:0.0,
      progressStatus:0,
      progressController:true,  // true就轮询，false就终止轮询
    }
  },
  methods: {
    downloadTemple(){
      let a=document.createElement('a')
      let name="importCustomer.xlsx"
      a.href="./static/"+name
      a.download="客户导入模板.xlsx"
      a.style.display="none"
      document.body.appendChild(a)
      a.click()
      a.remove()
    },
    uploadCheck(file){
      return new Promise((resolve, reject) => {
        const fileSuffix = file.name.substring(file.name.lastIndexOf(".") + 1);
        const whiteList = ["xls", "xlsx"];
        if (whiteList.indexOf(fileSuffix) === -1) {
          this.$message.error('请上传表格');
          return reject(false);
        }
        return resolve(true);
      })
    },
    getProgress(){
      httpGet("test/getProgress", {key:this.progressKey}).then(res => {
        if (res.ret ===0){
          this.progressNum = Number((res.datas.progress*100).toFixed(2))
          this.progressStatus = res.datas.status  // 处理状态  0是未开始   1是处理中   2是处理完毕     3是报错了

          if (res.datas.status!==2 && res.datas.status!==3){
            setTimeout(()=>{
              if (this.progressController){
                this.getProgress()
              }
            },1000) // 一秒轮询一次结果集
          }else{
            this.importLoading=false
            this.$refs.upload.clearFiles()
            if (res.datas.status===2){
              this.progressNum = 100.0
              this.$message.success("导入成功")
              this.refreshParentList()
            }
          }
        }else{
          this.importLoading=false
          this.$refs.upload.clearFiles()
          this.progressStatus = 3
          this.$message.error(res.msg);
        }
        this.errorTableShowFunc(res.datas.results)
      })
    },
    importTable(file){
      this.progressController = true
      this.progressKey = null;
      this.progressStatus = 0
      this.progressNum = 0.0
      this.errorTableShow = false
      this.errorTableData = []
      this.importLoading=true
      const formData = new FormData()
      formData.append("file", file.file);
      httpJson("test/importExcel", formData).then(res => {
        if (res.ret ===0){
          this.progressKey = res.datas
          this.getProgress()
        }else{
          this.$message.error(res.msg);
        }
      })
    },
    errorTableShowFunc(data){
      this.errorTableShow = true
      this.errorTableData = data
    },
    refreshParentList(){
      this.$emit('refresh')
    },
    show(){
      this.customerDialogVisible = true
    },
    close(){
      this.importLoading = false
      this.progressController = false
      this.errorTableShow = false
      this.progressKey = null;
      this.progressStatus = 0
      this.progressNum = 0.0
      this.errorTableData = []
      this.customerDialogVisible = false
    },
  },
  computed: {

  },
  watch: {

  },
  mounted() {
  },
  created() {

  }
}

</script>
<style scoped>

/deep/ .el-progress-bar{
  width: 95%;
}
</style>
