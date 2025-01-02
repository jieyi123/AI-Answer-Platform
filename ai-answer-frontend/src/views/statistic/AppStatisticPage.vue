<template>
  <div id="appStatisticPage">
    <a-row class="grid-demo" justify="start">
      <a-col :span="12">
        <h2>热门应用统计</h2>
        <v-chart
          :option="resultCountOptions"
          style="height: 300px; width: 500px"
        />
      </a-col>
      <a-col :span="12">
        <h2>应用回答结果统计</h2>
        <a-select
          v-model="value"
          :style="{ width: '320px' }"
          placeholder="Please select ..."
          @change="getAnswerResult"
        >
          <a-option
            v-for="item of appData"
            :value="item"
            :label="item.label"
            :key="item.value"
          />
        </a-select>
        <v-chart
          :option="resultAnswerCountOptions"
          style="height: 300px; width: 500px"
        />
      </a-col>
    </a-row>
  </div>
</template>
<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import { computed, onMounted, ref, watchEffect } from "vue";
import {getAppAnswerCountUsingGet, getAppAnswerResultCountUsingGet} from "@/api/appStatisticController";
import { Message } from "@arco-design/web-vue";
import { listAppVoByPageUsingPost } from "@/api/appController";

const appAnswerCountList = ref<API.AppAnswerCountVO[]>([]);
const appResultCountList = ref<API.AppResultCountVO[]>([]);
const defaultValue=ref();
const appData = ref([]);
const loadData = async () => {
  const res = await getAppAnswerCountUsingGet();
  if (res.data.code === 0 && res.data.data) {
    appAnswerCountList.value = res.data.data;
  } else {
    Message.error("获取图表数据失败" + res.data.message);
  }
};

onMounted(async () => {
  const result = await listAppVoByPageUsingPost({});
  if (result.data.code === 0 && result.data.data?.records) {
    appData.value = result.data.data.records.map((item) => ({
      label: item.appName,
      value: item.id,
    }));
    defaultValue.value=appData.value[0].label;
  }
});

watchEffect(() => {
  loadData();
});


const resultCountOptions = computed(() => {
  return {
    xAxis: {
      type: "category",
      data: appAnswerCountList.value.map((item) => item.appId),
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        data: appAnswerCountList.value.map((item) => item.answerCount),
        type: "bar",
      },
    ],
  };
});

const getAnswerResult = async (value: any) => {
  const res = await getAppAnswerResultCountUsingGet({
    appId: value.value,
  });
  if (res.data.code === 0 && res.data.data) {
    appResultCountList.value = res.data.data;
    console.log(appResultCountList.value)
  }
};

const resultAnswerCountOptions = computed(()=>{
  return{
    tooltip: {
      trigger: "item",
    },
    series: [
      {
        type: "pie",
        radius: "70%",
        data: appResultCountList.value.map((item)=>({name:item.resultName,value:item.resultCount})),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
})
</script>
<style scoped></style>
