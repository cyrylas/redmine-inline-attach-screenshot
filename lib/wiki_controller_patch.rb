module AttachScreenshotPlugin
  module WikiControllerPatch
    def self.included(base)
      base.class_eval do
        def update_with_plugin
          ret = update_without_plugin          
          if !@page.new_record? && params[:content].present? && @content.text == params[:content][:text]
            call_hook(:controller_wiki_edit_after_save, { :params => params, :page => @page})
          end
          return ret
        end
        alias_method_chain :update, :plugin
        
        
        def add_attachment_with_plugin
          ret = add_attachment_without_plugin
          call_hook(:controller_wiki_edit_after_save, { :params => params, :page => @page})
          return ret
        end
        alias_method_chain :add_attachment, :plugin
         
      end
    end
  end
end