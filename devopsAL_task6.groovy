job("task6_devopsAL_job_1"){
  description("Pull files from GitHub")
  scm{
    github("sanjaytripathi97/task3_devopsAL_sanjay","master")
  }
  triggers {
    scm("* * * * *")
  }
  steps{
    shell('''sudo rm -rvf * /web6
sudo mkdir /web6
sudo cp -rvf * /web6
''')
  }
}

job("task6_devopsAL_job_2"){
  description("launching pods according to the file")
  triggers {
    upstream("task6_devopsAL_job_1", "SUCCESS")
  }
  steps{
shell('''if ls /web | grep html
 then
  if sudo kubectl get pods | grep webserver
   then
    echo "webserver pods are running(HTML)"
    sudo python3 /web6/copy_to_pods.py
  else
    sudo kubectl create deployment webserver --image=sanjay874/webserver:v3
    sudo kubectl scale deployment webserver --replicas=2
    sleep 30
    sudo python3 /web/copy_to_pods.py
    sudo kubectl expose deployment webserver --port 80 --type NodePort
  fi
  
elif ls /web6 | grep php
 then
  if sudo kubectl get pods | grep webserver-php
   then
    echo "webserver pods are running(PHP)"
    sudo python3 /web6/copy_to_pods_php.py
  else
    sudo kubectl create deployment webserver-php --image=sanjay874/webserver:v3
    sudo kubectl scale deployment webserver-php --replicas=2
    sleep 30
    sudo python3 /web/copy_to_pods_php.py
    sudo kubectl expose deployment webserver-php --port 80 --type NodePort
  fi  
else
 echo "something went WRONG"
fi 
''')
  }
}

job("task6_devopsAL_job_3"){
    description("Testing WebPages")
	triggers{
		upstream('task6_devopsAL_job_2' , 'SUCCESS')
	}
	steps{
		shell('''status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.100:30459)
if [[ $status == 200 ]]
then
 echo "app is running fine"
 exit 1
else
 exit 0
fi
''')
 }
}

job("task6_devopsAL_job_4"){
  description("IF web-page is not working then sending mail to admin")

triggers {
    upstream("task6_devopsAL_job_3", "SUCCESS")
  }
  steps{
    shell('''sudo python3 /web6/mail_failure.py
''')
}
}

buildPipelineView("Build_Pipeline_devopsAL_6") {
    filterBuildQueue(true)
    filterExecutors(false)
    title("DevopsAL_6")
    displayedBuilds(1)
    selectedJob("task6_devopsAL_job_1")
    alwaysAllowManualTrigger(true)
    showPipelineParameters(true)
    refreshFrequency(5)
}