<template>
  <a-form
    :model="formSearchParams"
    :style="{ marginBottom: '20px' }"
    layout="inline"
    @submit="doSearch"
  >
    <a-form-item field="appName" label="应用名称">
      <a-input
        v-model="formSearchParams.appName"
        placeholder="请输入应用名称"
        allow-clear
      />
    </a-form-item>
    <a-form-item field="appDesc" label="应用描述">
      <a-input
        v-model="formSearchParams.appDesc"
        placeholder="请输入应用描述"
        allow-clear
      />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" html-type="submit" style="width: 100px">
        搜索
      </a-button>
    </a-form-item>
  </a-form>
  <a-table
    :columns="columns"
    :data="dataList"
    :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }"
    @page-change="onPageChange"
  >
    <template #appIcon="{ record }">
      <a-image width="64" :src="record.appIcon" />
    </template>
    <template #appType="{ record }">
      {{ APP_TYPE_MAP[record.appType] }}
    </template>
    <template #scoringStrategy="{ record }">
      {{ APP_SCORING_STRATEGY_MAP[record.scoringStrategy] }}
    </template>
    <template #reviewStatus="{ record }">
      {{ REVIEW_STATUS_MAP[record.reviewStatus] }}
    </template>
    <template #reviewTime="{ record }">
      {{
        record.reviewTime &&
        dayjs(record.reviewTime).format("YYYY-MM-DD HH:mm:ss")
      }}
    </template>
    <template #createTime="{ record }">
      {{ dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
    </template>
    <template #updateTime="{ record }">
      {{ dayjs(record.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
    </template>
    <template #optional="{ record }">
      <a-space>
        <a-button type="primary" status="success" @click="doReview(record)">
          审核
        </a-button>
        <a-button status="danger" @click="doDelete(record)">删除</a-button>
      </a-space>
      <a-modal
        v-model:visible="visible"
        title="审核信息"
        @cancel="handleCancel"
        @ok="handleReview"
      >
        <a-form :model="form">
          <a-form-item field="post" label="审核状态">
            <a-select v-model="form.reviewStatus" placeholder="请选择审核状态">
              <a-option :value="REVIEW_STATUS_ENUM.PASS">通过</a-option>
              <a-option :value="REVIEW_STATUS_ENUM.REJECT">拒绝</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="name" label="审核意见">
            <a-input v-model="form.reviewMessage" />
          </a-form-item>
        </a-form>
      </a-modal>
    </template>
  </a-table>
</template>
<script setup lang="ts">
import { ref, watchEffect, reactive } from "vue";
import {
  deleteAppUsingPost,
  listAppByPageUsingPost,
  reviewAppUsingPost,
} from "@/api/appController";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import { dayjs } from "@arco-design/web-vue/es/_utils/date";
import {
  APP_SCORING_STRATEGY_MAP,
  APP_TYPE_MAP,
  REVIEW_STATUS_ENUM,
  REVIEW_STATUS_MAP,
} from "@/constant/app";

const formSearchParams = ref<API.AppQueryRequest>({});
// 初始化搜索条件（不应该被修改）
const initSearchParams = {
  current: 1,
  pageSize: 10,
};
const searchParams = ref<API.AppQueryRequest>({
  ...initSearchParams,
});
const dataList = ref<API.App[]>([]);
const total = ref<number>(0);
const visible = ref(false);
/**
 * 加载数据
 */
const loadData = async () => {
  const res = await listAppByPageUsingPost(searchParams.value);
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || [];
    total.value = res.data.data?.total || 0;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};
/**
 * 执行搜索
 */
const doSearch = () => {
  searchParams.value = {
    ...initSearchParams,
    ...formSearchParams.value,
  };
};
/**
 * 当分页变化时，改变搜索条件，触发数据加载
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};
/**
 * 删除
 * @param record
 */
const doDelete = async (record: API.App) => {
  if (!record.id) {
    return;
  }
  const res = await deleteAppUsingPost({
    id: record.id,
  });
  if (res.data.code === 0) {
    loadData();
  } else {
    message.error("删除失败，" + res.data.message);
  }
};

const form = reactive({
  id: "",
  reviewStatus: "",
  reviewMessage: "",
});

const handleReview = async () => {
  const res = await reviewAppUsingPost({
    ...form,
  });
  if (res.data.code === 0) {
    message.success("审核完成");
    loadData();
  } else {
    message.error("审核失败，" + res.data.message);
  }
};
const handleCancel = () => {
  visible.value = false;
};
/**
 * 审核
 * @param record
 * @param reviewStatus
 * @param reviewMessage
 */
const doReview = (record: API.App) => {
  if (!record.id) {
    return;
  }
  form.id = record.id;
  form.reviewStatus = record.reviewStatus != 0 ? record.reviewStatus : "";
  form.reviewMessage = record.reviewMessage;
  visible.value = true;
};
/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
// 表格列配置
const columns = [
  {
    title: "id",
    dataIndex: "id",
  },
  {
    title: "名称",
    dataIndex: "appName",
  },
  {
    title: "描述",
    dataIndex: "appDesc",
  },
  {
    title: "图标",
    dataIndex: "appIcon",
    slotName: "appIcon",
  },
  {
    title: "应用类型",
    dataIndex: "appType",
    slotName: "appType",
  },
  {
    title: "评分策略",
    dataIndex: "scoringStrategy",
    slotName: "scoringStrategy",
  },
  {
    title: "审核状态",
    dataIndex: "reviewStatus",
    slotName: "reviewStatus",
  },
  {
    title: "审核信息",
    dataIndex: "reviewMessage",
  },
  {
    title: "审核时间",
    dataIndex: "reviewTime",
    slotName: "reviewTime",
  },
  {
    title: "审核人 id",
    dataIndex: "reviewerId",
  },
  {
    title: "用户 id",
    dataIndex: "userId",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];
</script>
