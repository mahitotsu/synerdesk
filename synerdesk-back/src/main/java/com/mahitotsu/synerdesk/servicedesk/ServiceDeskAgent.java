package com.mahitotsu.synerdesk.servicedesk;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.AgentDefinition;

import software.amazon.awssdk.services.bedrockagentruntime.model.CollaboratorConfiguration;

@Component
public class ServiceDeskAgent implements AgentDefinition {

    @Override
    public String getFoundationModel() {
        // return "apac.amazon.nova-micro-v1:0";
        // return "apac.amazon.nova-pro-v1:0";
        // return "anthropic.claude-3-haiku-20240307-v1:0";
        return "apac.anthropic.claude-3-5-sonnet-20241022-v2:0";
    }

    @Override
    public String getInstruction() {
        return """
                あなたは定められた手順を忠実に実行するサービスデスクのアシスタントです。
                ユーザーからの要求を受け取ったら、以下の手順で作業を行います。

                <steps>
                1. ユーザーの要求を英語に翻訳します。
                2. 英語に翻訳したユーザーの要求を分析し、要求を満たすために必要なタスクのリストを作成します。
                3. 作成したタスクのリストを先頭から順番に実行して、結果を取得します。
                4. 全てのタスクの結果が取得出来たら、最終的な結論を作成します。
                5. ユーザーに最終的な出力を返します。
                </steps>

                最終的な出力のフォーマットはJSON形式にしてください。プレーンテキストや追加の情報は不要です。
                以下に出力のサンプルを示します。
                <sample>
                {
                    "tasks": [
                        {
                            "task": "実行したタスク",
                            "agent": "タスクを実行したエージェントの名前",
                            "result": "取得した結果"
                        },
                        // 実行したすべてのタスクの「実行したタスク」と「取得した結果」を出力する
                    ], 
                    "conclusion": "最終的な結論"
                }
                </sample>
                """;
    }

    @Override
    public Collection<CollaboratorConfiguration> getCollaboratorConfigurations() {

        return Arrays.asList(
                CollaboratorConfiguration.builder()
                        .collaboratorName("HealthCheckAgent")
                        .collaboratorInstruction("""
                                システムの健全性に関する質問を解決します。
                                確認対象は複数指定することが可能です。
                                """)
                        .build(),
                CollaboratorConfiguration.builder()
                        .collaboratorName("TranslateAgent")
                        .collaboratorInstruction("""
                                    指定された文章を指定された言語に翻訳します。
                                    以下のフォーマットで依頼してください。
                                    -----
                                    「<ここに翻訳したい文章>」を<翻訳後の言語>に翻訳してください。
                                """)
                        .build());
    }

}
