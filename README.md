## Description
PoC Helm Agent is a scheduled job that manage status of your Kubernete releases.

## Configuration 

Agent environment variables:

- **HELM_COMMAND**: helm binary command. **Default**: /opt/homebrew/bin/helm
- **HELM_REPO**: helm package repository. **Default**: chartmuseum

To configure the chart repository we must execute for example:

```shell
helm repo add chartmuseum http://localhost:8088
```